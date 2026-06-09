<?php

namespace App\Controller;

use App\Entity\User;
use App\Repository\UserRepository;
use Doctrine\DBAL\Exception\UniqueConstraintViolationException;
use Doctrine\ORM\EntityManagerInterface;
use OpenApi\Attributes as OA;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Throwable;

#[OA\Tag(name: 'Auth', description: 'Registro y autenticación')]
class AuthController extends AbstractController
{
    #[Route('/api/register', methods: ['POST'])]
    #[OA\Post(
        path: '/api/register',
        summary: 'Registrar usuario',
        security: [],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(ref: '#/components/schemas/RegisterInput')
        ),
        responses: [
            new OA\Response(response: 201, description: 'Usuario registrado correctamente'),
            new OA\Response(response: 400, description: 'Email y password requeridos'),
            new OA\Response(response: 409, description: 'El email ya está registrado'),
        ]
    )]
    public function register(
        Request $request,
        EntityManagerInterface $em,
        UserPasswordHasherInterface $passwordHasher,
        UserRepository $userRepository
    ): JsonResponse {

        try {
            $data = json_decode($request->getContent(), true);

            if (!is_array($data) || !isset($data['email'], $data['password'])) {
                return $this->json([
                    'error' => 'Email y password requeridos'
                ], Response::HTTP_BAD_REQUEST);
            }

            $email = trim((string) $data['email']);
            $password = (string) $data['password'];

            if ($error = self::validateRegisterEmail($email)) {
                return $this->json(['error' => $error], Response::HTTP_BAD_REQUEST);
            }

            if ($error = self::validateRegisterPassword($password)) {
                return $this->json(['error' => $error], Response::HTTP_BAD_REQUEST);
            }

            if ($userRepository->findOneBy(['email' => $email])) {
                return $this->json(['error' => 'El email ya está registrado'], Response::HTTP_CONFLICT);
            }

            $user = new User();
            $user->setEmail($email);
            $user->setPassword($passwordHasher->hashPassword($user, $password));
            $user->setRoles(['ROLE_USER']);

            $em->persist($user);
            $em->flush();

            return $this->json([
                'message' => 'Usuario registrado correctamente'
            ], Response::HTTP_CREATED);
        } catch (UniqueConstraintViolationException) {
            return $this->json(['error' => 'El email ya está registrado'], Response::HTTP_CONFLICT);
        } catch (Throwable $e) {
            return $this->json([
                'error' => 'No se pudo registrar el usuario',
            ], Response::HTTP_INTERNAL_SERVER_ERROR);
        }
    }

    private static function validateRegisterEmail(string $email): ?string
    {
        if ($email === '') {
            return 'El email es obligatorio';
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            return 'Formato de email no válido';
        }

        if (strlen($email) > 180) {
            return 'El email es demasiado largo';
        }

        return null;
    }

    private static function validateRegisterPassword(string $password): ?string
    {
        if ($password === '') {
            return 'La contraseña es obligatoria';
        }

        if (strlen($password) < 6) {
            return 'La contraseña debe tener al menos 6 caracteres';
        }

        if (strlen($password) > 128) {
            return 'La contraseña es demasiado larga';
        }

        return null;
    }
}
