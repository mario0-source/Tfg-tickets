<?php

namespace App\Controller;

use App\Entity\Ticket;
use App\Repository\TicketRepository;
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
        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        $tickets = $ticketRepository->findBy([
            'user' => $user
        ]);

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

    // 👇 MÉTODO NUEVO
    #[Route('/api/stats/monthly-expense', methods: ['GET'])]
    public function monthlyExpense(
        TicketRepository $ticketRepository
    ): JsonResponse {

        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        $tickets = $ticketRepository->findBy([
            'user' => $user
        ]);

        $total = 0;
        $categories = [];

        foreach ($tickets as $ticket) {

            $total += $ticket->getPrecio();

            if ($ticket->getCategoria()) {
                $categories[] = $ticket->getCategoria();
            }
        }

        return $this->json([
            'total' => $total,
            'variationPercent' => 12,
            'ticketsCount' => count($tickets),
            'categoriesCount' => count(array_unique($categories))
        ]);
    }

    #[Route('/api/tickets', methods: ['POST'])]
    public function createTicket(
        Request $request,
        EntityManagerInterface $em
    ): JsonResponse {

        $data = json_decode($request->getContent(), true);

        if (!isset($data['nombre'], $data['precio'])) {
            return $this->json([
                'error' => 'Faltan campos obligatorios'
            ], 400);
        }

        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        if (!$user) {
            return $this->json([
                'error' => 'Usuario no autenticado'
            ], 401);
        }

        $ticket = new Ticket();
        $ticket->setNombre($data['nombre']);
        $ticket->setPrecio($data['precio']);
        $ticket->setCategoria($data['categoria'] ?? null);
        $ticket->setFecha(new \DateTime());

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
                'user_id' => $user->getId(),
                'user_email' => $user->getEmail()
            ]
        ], 201);
    }
}
