<?php

namespace App\Controller;

use App\Entity\Ticket;
use App\Repository\TicketRepository;
use Doctrine\ORM\EntityManagerInterface;
use OpenApi\Attributes as OA;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[OA\Tag(name: 'Tickets', description: 'Gestión de tickets de compra')]
final class TicketController extends AbstractController
{
    #[Route('/api/tickets', methods: ['GET'])]
    #[OA\Get(
        path: '/api/tickets',
        summary: 'Listar tickets del usuario autenticado',
        description: 'Devuelve todos los tickets del usuario ordenados por fecha descendente. Usa el campo `id` de la respuesta para probar GET/PUT por ID.',
        responses: [
            new OA\Response(
                response: 200,
                description: 'Lista de tickets',
                content: new OA\JsonContent(
                    type: 'array',
                    items: new OA\Items(
                        properties: [
                            new OA\Property(property: 'id', type: 'integer', example: 1),
                            new OA\Property(property: 'nombre', type: 'string', example: 'Mercadona'),
                            new OA\Property(property: 'precio', type: 'number', format: 'float', example: 24.50),
                            new OA\Property(property: 'categoria', type: 'string', example: 'Alimentación'),
                            new OA\Property(property: 'fecha', type: 'string', example: '2026-05-24 12:30:00'),
                            new OA\Property(
                                property: 'productos',
                                type: 'array',
                                items: new OA\Items(
                                    properties: [
                                        new OA\Property(property: 'nombre', type: 'string', example: 'Leche entera'),
                                        new OA\Property(property: 'precio', type: 'number', format: 'float', example: 1.25),
                                    ],
                                    type: 'object'
                                )
                            ),
                        ],
                        type: 'object'
                    )
                )
            ),
            new OA\Response(response: 401, description: 'No autenticado'),
        ]
    )]
    public function getTickets(TicketRepository $ticketRepository): JsonResponse
    {
        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        $tickets = $ticketRepository->findBy(['user' => $user], ['fecha' => 'DESC']);

        $data = array_map(
            fn (Ticket $ticket) => $this->ticketToArray($ticket),
            $tickets
        );

        return $this->json($data);
    }

    #[Route('/api/tickets/{id}', methods: ['GET'], requirements: ['id' => '\d+'])]
    #[OA\Get(
        path: '/api/tickets/{id}',
        summary: 'Obtener un ticket por ID',
        parameters: [
            new OA\Parameter(
                name: 'id',
                in: 'path',
                required: true,
                description: 'ID del ticket (consulta GET /api/tickets para ver IDs válidos de tu usuario)',
                schema: new OA\Schema(type: 'integer', example: 1)
            ),
        ],
        responses: [
            new OA\Response(response: 200, description: 'Detalle del ticket'),
            new OA\Response(response: 404, description: 'Ticket no encontrado o no pertenece al usuario'),
            new OA\Response(response: 401, description: 'No autenticado'),
        ]
    )]
    public function getTicket(int $id, TicketRepository $ticketRepository): JsonResponse
    {
        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        $ticket = $ticketRepository->find($id);

        if (!$ticket || $ticket->getUser()?->getId() !== $user->getId()) {
            return $this->json(['error' => 'Ticket no encontrado'], Response::HTTP_NOT_FOUND);
        }

        return $this->json($this->ticketToArray($ticket));
    }

    #[Route('/api/stats/monthly-expense', methods: ['GET'])]
    #[OA\Get(
        path: '/api/stats/monthly-expense',
        summary: 'Estadísticas de gasto mensual',
        responses: [
            new OA\Response(
                response: 200,
                description: 'Totales y contadores',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'total', type: 'number', format: 'float', example: 156.75),
                        new OA\Property(property: 'variationPercent', type: 'integer', example: 12),
                        new OA\Property(property: 'ticketsCount', type: 'integer', example: 8),
                        new OA\Property(property: 'categoriesCount', type: 'integer', example: 3),
                    ],
                    type: 'object'
                )
            ),
            new OA\Response(response: 401, description: 'No autenticado'),
        ]
    )]
    public function monthlyExpense(TicketRepository $ticketRepository): JsonResponse
    {
        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        $tickets = $ticketRepository->findBy(['user' => $user]);

        $total = 0.0;
        $categories = [];

        foreach ($tickets as $ticket) {
            $total += $ticket->getPrecio() ?? 0.0;

            if ($ticket->getCategoria()) {
                $categories[] = $ticket->getCategoria();
            }
        }

        return $this->json([
            'total' => round($total, 2),
            'variationPercent' => 12,
            'ticketsCount' => count($tickets),
            'categoriesCount' => count(array_unique($categories)),
        ]);
    }

    #[Route('/api/tickets', methods: ['POST'])]
    #[OA\Post(
        path: '/api/tickets',
        summary: 'Crear un ticket',
        description: 'Campos obligatorios: `nombre` (tienda) y `precio` (total). `categoria` y `productos` son opcionales.',
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(ref: '#/components/schemas/TicketInput')
        ),
        responses: [
            new OA\Response(response: 201, description: 'Ticket creado correctamente'),
            new OA\Response(response: 400, description: 'Faltan campos obligatorios (nombre y precio)'),
            new OA\Response(response: 401, description: 'No autenticado'),
        ]
    )]
    public function createTicket(
        Request $request,
        EntityManagerInterface $em
    ): JsonResponse {
        $data = json_decode($request->getContent(), true);

        if (!isset($data['nombre'], $data['precio'])) {
            return $this->json(['error' => 'Faltan campos obligatorios'], 400);
        }

        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        if (!$user) {
            return $this->json(['error' => 'Usuario no autenticado'], 401);
        }

        $precio = (float) $data['precio'];
        $productos = $this->normalizeProductos($data['productos'] ?? [], $precio);

        $ticket = new Ticket();
        $ticket->setNombre((string) $data['nombre']);
        $ticket->setPrecio($precio);
        $ticket->setCategoria($data['categoria'] ?? null);
        $ticket->setFecha(new \DateTime());
        $ticket->setProductos($productos);
        $ticket->setUser($user);

        $em->persist($ticket);
        $em->flush();

        return $this->json([
            'message' => 'Ticket creado correctamente',
            'ticket' => $this->ticketToArray($ticket),
        ], 201);
    }

    #[Route('/api/tickets/{id}', methods: ['PUT'], requirements: ['id' => '\d+'])]
    #[OA\Put(
        path: '/api/tickets/{id}',
        summary: 'Actualizar un ticket existente',
        description: 'Debes enviar el body JSON completo (nombre y precio son obligatorios). El ID debe existir y pertenecer a tu usuario: si recibes 404, prueba otro ID de GET /api/tickets. Si recibes 400, revisa que el body incluya nombre y precio.',
        parameters: [
            new OA\Parameter(
                name: 'id',
                in: 'path',
                required: true,
                description: 'ID del ticket a actualizar',
                schema: new OA\Schema(type: 'integer', example: 1)
            ),
        ],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(ref: '#/components/schemas/TicketInput')
        ),
        responses: [
            new OA\Response(response: 200, description: 'Ticket actualizado correctamente'),
            new OA\Response(response: 400, description: 'Faltan campos obligatorios en el body'),
            new OA\Response(response: 404, description: 'Ticket no encontrado'),
            new OA\Response(response: 401, description: 'No autenticado'),
        ]
    )]
    public function updateTicket(
        int $id,
        Request $request,
        TicketRepository $ticketRepository,
        EntityManagerInterface $em
    ): JsonResponse {
        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        $ticket = $ticketRepository->find($id);

        if (!$ticket || $ticket->getUser()?->getId() !== $user->getId()) {
            return $this->json(['error' => 'Ticket no encontrado'], Response::HTTP_NOT_FOUND);
        }

        $data = json_decode($request->getContent(), true);

        if (!isset($data['nombre'], $data['precio'])) {
            return $this->json(['error' => 'Faltan campos obligatorios'], Response::HTTP_BAD_REQUEST);
        }

        $precio = (float) $data['precio'];
        $productos = $this->normalizeProductos($data['productos'] ?? [], $precio);

        $ticket->setNombre((string) $data['nombre']);
        $ticket->setPrecio($precio);
        $ticket->setCategoria($data['categoria'] ?? null);
        $ticket->setProductos($productos);

        $em->flush();

        return $this->json([
            'message' => 'Ticket actualizado correctamente',
            'ticket' => $this->ticketToArray($ticket),
        ]);
    }

    #[Route('/api/tickets/{id}', methods: ['DELETE'], requirements: ['id' => '\d+'])]
    #[OA\Delete(
        path: '/api/tickets/{id}',
        summary: 'Eliminar un ticket',
        description: 'Elimina un ticket del usuario autenticado. Usa un ID de GET /api/tickets.',
        parameters: [
            new OA\Parameter(
                name: 'id',
                in: 'path',
                required: true,
                description: 'ID del ticket a eliminar',
                schema: new OA\Schema(type: 'integer', example: 1)
            ),
        ],
        responses: [
            new OA\Response(response: 200, description: 'Ticket eliminado correctamente'),
            new OA\Response(response: 404, description: 'Ticket no encontrado'),
            new OA\Response(response: 401, description: 'No autenticado'),
        ]
    )]
    public function deleteTicket(
        int $id,
        TicketRepository $ticketRepository,
        EntityManagerInterface $em
    ): JsonResponse {
        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        $ticket = $ticketRepository->find($id);

        if (!$ticket || $ticket->getUser()?->getId() !== $user->getId()) {
            return $this->json(['error' => 'Ticket no encontrado'], Response::HTTP_NOT_FOUND);
        }

        $em->remove($ticket);
        $em->flush();

        return $this->json(['message' => 'Ticket eliminado correctamente']);
    }

    private function ticketToArray(Ticket $ticket): array
    {
        return [
            'id' => $ticket->getId(),
            'nombre' => $ticket->getNombre(),
            'precio' => $ticket->getPrecio(),
            'categoria' => $ticket->getCategoria(),
            'fecha' => $ticket->getFecha()?->format('Y-m-d H:i:s'),
            'productos' => $this->formatProductos($ticket->getProductos()),
        ];
    }

    /**
     * @param array<int, mixed> $productos
     * @return array<int, array{nombre: string, precio: float}>
     */
    private function normalizeProductos(array $productos, float $ticketPrecio): array
    {
        $normalized = [];

        foreach ($productos as $producto) {
            if (!is_array($producto)) {
                continue;
            }

            $nombre = trim((string) ($producto['nombre'] ?? ''));
            if ($nombre === '') {
                continue;
            }

            $precio = isset($producto['precio']) && $producto['precio'] !== ''
                ? (float) $producto['precio']
                : null;

            $normalized[] = [
                'nombre' => $nombre,
                'precio' => $precio,
            ];
        }

        if (count($normalized) === 1 && $normalized[0]['precio'] === null) {
            $normalized[0]['precio'] = $ticketPrecio;
        }

        return array_values(array_map(
            static fn (array $item): array => [
                'nombre' => $item['nombre'],
                'precio' => round((float) ($item['precio'] ?? 0.0), 2),
            ],
            array_filter($normalized, static fn (array $item): bool => ($item['precio'] ?? 0) > 0)
        ));
    }

    /**
     * @param array<int, mixed>|null $productos
     * @return array<int, array{nombre: string, precio: float}>
     */
    private function formatProductos(?array $productos): array
    {
        if ($productos === null || $productos === []) {
            return [];
        }

        $formatted = [];

        foreach ($productos as $producto) {
            if (!is_array($producto)) {
                continue;
            }

            $nombre = trim((string) ($producto['nombre'] ?? ''));
            if ($nombre === '') {
                continue;
            }

            $formatted[] = [
                'nombre' => $nombre,
                'precio' => round((float) ($producto['precio'] ?? 0.0), 2),
            ];
        }

        return $formatted;
    }
}
