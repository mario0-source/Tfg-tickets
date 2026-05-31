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

        $data = json_decode($request->getContent(), true);

        if (!isset($data['email'], $data['password'])) {
            return $this->json([
                'error' => 'Email y password requeridos'
            ], 400);
        }

        $email = trim((string) $data['email']);

        if ($userRepository->findOneBy(['email' => $email])) {
            return $this->json(['error' => 'El email ya está registrado'], Response::HTTP_CONFLICT);
        }

        $user = new User();

        $user->setEmail($email);

        $hashedPassword = $passwordHasher->hashPassword(
            $user,
            (string) $data['password']
        );

        $user->setPassword($hashedPassword);

        $user->setRoles(['ROLE_USER']);

        $em->persist($user);

        try {
            $em->flush();
        } catch (UniqueConstraintViolationException) {
            return $this->json(['error' => 'El email ya está registrado'], Response::HTTP_CONFLICT);
        }

        return $this->json([
            'message' => 'Usuario registrado correctamente'
        ], 201);
    }
}
