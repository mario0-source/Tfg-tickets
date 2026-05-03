<?php

namespace App\Controller;

use App\Entity\Ticket;
use App\Repository\TicketRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

final class TicketController extends AbstractController
{
    #[Route('/api/tickets', methods: ['GET'])]
    public function getTickets(TicketRepository $ticketRepository): JsonResponse
    {
        $tickets = $ticketRepository->findAll();

        $data = [];

        foreach ($tickets as $ticket) {
            $data[] = [
                'id' => $ticket->getId(),
                'nombre' => $ticket->getNombre(),
                'precio' => $ticket->getPrecio(),
                'categoria' => $ticket->getCategoria(),
                'fecha' => $ticket->getFecha()?->format('Y-m-d H:i:s'),
                'user_id' => $ticket->getUser()?->getId(),
                'user_email' => $ticket->getUser()?->getEmail(),
            ];
        }

        return $this->json($data);
    }

    #[Route('/api/tickets', methods: ['POST'])]
    public function createTicket(
        Request $request,
        EntityManagerInterface $em,
        UserRepository $userRepository
    ): JsonResponse {

        $data = json_decode($request->getContent(), true);

        if (!isset($data['nombre'], $data['precio'])) {
            return $this->json([
                'error' => 'Faltan campos obligatorios'
            ], 400);
        }

        $ticket = new Ticket();
        $ticket->setNombre($data['nombre']);
        $ticket->setPrecio($data['precio']);
        $ticket->setCategoria($data['categoria'] ?? null);
        $ticket->setFecha(new \DateTime());

        // 🔥 MEJORADO: usuario desde request (NO hardcode)
        if (!isset($data['user_id'])) {
            return $this->json(['error' => 'user_id requerido'], 400);
        }

        $user = $userRepository->find($data['user_id']);

        if (!$user) {
            return $this->json(['error' => 'Usuario no encontrado'], 404);
        }

        $ticket->setUser($user);

        $em->persist($ticket);
        $em->flush();

        return $this->json([
            'message' => 'Ticket creado correctamente',
            'ticket' => [
                'id' => $ticket->getId(),
                'nombre' => $ticket->getNombre(),
                'precio' => $ticket->getPrecio(),
                'categoria' => $ticket->getCategoria(),
                'fecha' => $ticket->getFecha()->format('Y-m-d H:i:s'),
                'user_id' => $user->getId()
            ]
        ], 201);
    }
}
