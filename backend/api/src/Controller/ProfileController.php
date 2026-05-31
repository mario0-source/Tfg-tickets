<?php

namespace App\Controller;

use App\Repository\TicketRepository;
use OpenApi\Attributes as OA;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Attribute\Route;

#[OA\Tag(name: 'Profile', description: 'Perfil y estadísticas del usuario (solo lectura)')]
final class ProfileController extends AbstractController
{
    #[Route('/api/profile', methods: ['GET'])]
    #[OA\Get(
        path: '/api/profile',
        summary: 'Perfil financiero del usuario (solo lectura)',
        responses: [
            new OA\Response(
                response: 200,
                description: 'Estadísticas del perfil',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'email', type: 'string', example: 'test@test.com'),
                        new OA\Property(property: 'ticketsCount', type: 'integer', example: 8),
                        new OA\Property(property: 'totalSpent', type: 'number', format: 'float', example: 256.40),
                        new OA\Property(property: 'avgSpendPerTicket', type: 'number', format: 'float', example: 32.05),
                        new OA\Property(property: 'topCategory', type: 'string', example: 'Alimentación'),
                        new OA\Property(property: 'monthlySpent', type: 'number', format: 'float', example: 78.20),
                    ],
                    type: 'object'
                )
            ),
            new OA\Response(response: 401, description: 'No autenticado'),
        ]
    )]
    public function profile(TicketRepository $ticketRepository): JsonResponse
    {
        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        $tickets = $ticketRepository->findBy(['user' => $user]);

        $totalSpent = 0.0;
        $monthlySpent = 0.0;
        $categoryTotals = [];
        $currentMonth = (new \DateTime())->format('Y-m');

        foreach ($tickets as $ticket) {
            $price = $ticket->getPrecio() ?? 0.0;
            $totalSpent += $price;

            $fecha = $ticket->getFecha();
            if ($fecha && $fecha->format('Y-m') === $currentMonth) {
                $monthlySpent += $price;
            }

            $categoria = $ticket->getCategoria() ?: 'General';
            $categoryTotals[$categoria] = ($categoryTotals[$categoria] ?? 0) + 1;
        }

        $ticketsCount = count($tickets);
        $avgSpendPerTicket = $ticketsCount > 0 ? round($totalSpent / $ticketsCount, 2) : 0.0;

        $topCategory = 'General';
        if ($categoryTotals !== []) {
            arsort($categoryTotals);
            $topCategory = (string) array_key_first($categoryTotals);
        }

        return $this->json([
            'email' => $user->getEmail(),
            'ticketsCount' => $ticketsCount,
            'totalSpent' => round($totalSpent, 2),
            'avgSpendPerTicket' => $avgSpendPerTicket,
            'topCategory' => $topCategory,
            'monthlySpent' => round($monthlySpent, 2),
        ]);
    }
}
