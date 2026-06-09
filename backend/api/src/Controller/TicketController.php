<?php

namespace App\Controller;

use App\Entity\Ticket;
use App\Repository\TicketRepository;
use Doctrine\ORM\EntityManagerInterface;
use Throwable;
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

        $currentMonth = (new \DateTime())->format('Y-m');
        $previousMonth = (new \DateTime('first day of last month'))->format('Y-m');

        $currentMonthTotal = 0.0;
        $previousMonthTotal = 0.0;
        $ticketsThisMonth = 0;
        $categories = [];

        foreach ($tickets as $ticket) {
            $price = $ticket->getPrecio() ?? 0.0;
            $fecha = $ticket->getFecha();

            if ($fecha === null) {
                continue;
            }

            $monthKey = $fecha->format('Y-m');

            if ($monthKey === $currentMonth) {
                $currentMonthTotal += $price;
                ++$ticketsThisMonth;

                if ($ticket->getCategoria()) {
                    $categories[] = $ticket->getCategoria();
                }
            } elseif ($monthKey === $previousMonth) {
                $previousMonthTotal += $price;
            }
        }

        $variationPercent = 0;
        if ($previousMonthTotal > 0) {
            $variationPercent = (int) round((($currentMonthTotal - $previousMonthTotal) / $previousMonthTotal) * 100);
        } elseif ($currentMonthTotal > 0) {
            $variationPercent = 100;
        }

        return $this->json([
            'total' => round($currentMonthTotal, 2),
            'variationPercent' => $variationPercent,
            'ticketsCount' => $ticketsThisMonth,
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
        try {
            $data = json_decode($request->getContent(), true);

            if (!is_array($data)) {
                return $this->json(['error' => 'JSON no válido'], Response::HTTP_BAD_REQUEST);
            }

            $validated = self::validateTicketPayload($data);

            if ($validated['error'] !== null) {
                return $this->json(['error' => $validated['error']], Response::HTTP_BAD_REQUEST);
            }

            /** @var \App\Entity\User $user */
            $user = $this->getUser();

            if (!$user) {
                return $this->json(['error' => 'Usuario no autenticado'], Response::HTTP_UNAUTHORIZED);
            }

            $precio = $validated['precio'];
            $productos = $this->normalizeProductos($validated['productos'], $precio);

            $ticket = new Ticket();
            $ticket->setNombre((string) $validated['nombre']);
            $ticket->setPrecio($precio);
            $ticket->setCategoria($validated['categoria']);
            $ticket->setFecha($validated['fecha'] !== null ? \DateTime::createFromInterface($validated['fecha']) : new \DateTime());
            $ticket->setProductos($productos);
            $ticket->setUser($user);

            $em->persist($ticket);
            $em->flush();

            return $this->json([
                'message' => 'Ticket creado correctamente',
                'ticket' => $this->ticketToArray($ticket),
            ], Response::HTTP_CREATED);
        } catch (Throwable) {
            return $this->json(['error' => 'No se pudo crear el ticket'], Response::HTTP_INTERNAL_SERVER_ERROR);
        }
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

        if (!is_array($data)) {
            return $this->json(['error' => 'JSON no válido'], Response::HTTP_BAD_REQUEST);
        }

        try {
            $validated = self::validateTicketPayload($data);

            if ($validated['error'] !== null) {
                return $this->json(['error' => $validated['error']], Response::HTTP_BAD_REQUEST);
            }

            $precio = $validated['precio'];
            $productos = $this->normalizeProductos($validated['productos'], $precio);

            $ticket->setNombre((string) $validated['nombre']);
            $ticket->setPrecio($precio);
            $ticket->setCategoria($validated['categoria']);
            if ($validated['fecha'] !== null) {
                $ticket->setFecha(\DateTime::createFromInterface($validated['fecha']));
            }
            $ticket->setProductos($productos);

            $em->flush();

            return $this->json([
                'message' => 'Ticket actualizado correctamente',
                'ticket' => $this->ticketToArray($ticket),
            ]);
        } catch (Throwable) {
            return $this->json(['error' => 'No se pudo actualizar el ticket'], Response::HTTP_INTERNAL_SERVER_ERROR);
        }
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

    /**
     * @param array<string, mixed> $data
     * @return array{error: ?string, nombre: ?string, precio: ?float, categoria: ?string, productos: array<int, array{nombre: string, precio: ?float}>, fecha: ?\DateTimeInterface}
     */
    private static function validateTicketPayload(array $data, bool $requireAll = true): array
    {
        $nombre = isset($data['nombre']) ? trim((string) $data['nombre']) : '';
        $precioRaw = $data['precio'] ?? null;
        $categoria = isset($data['categoria']) ? trim((string) $data['categoria']) : null;
        $productos = is_array($data['productos'] ?? null) ? $data['productos'] : [];
        $fechaRaw = isset($data['fecha']) ? trim((string) $data['fecha']) : '';

        if ($requireAll && $nombre === '') {
            return self::ticketValidationError('El nombre de la tienda es obligatorio');
        }

        if ($requireAll && ($precioRaw === null || $precioRaw === '')) {
            return self::ticketValidationError('El precio es obligatorio');
        }

        if ($precioRaw !== null && $precioRaw !== '' && !is_numeric($precioRaw)) {
            return self::ticketValidationError('El precio debe ser un número válido');
        }

        $precio = ($precioRaw === null || $precioRaw === '') ? null : (float) $precioRaw;

        if ($precio !== null && $precio <= 0) {
            return self::ticketValidationError('El precio debe ser mayor que 0');
        }

        if ($precio !== null && $precio > 999999.99) {
            return self::ticketValidationError('El precio es demasiado alto');
        }

        if ($nombre !== '' && strlen($nombre) > 120) {
            return self::ticketValidationError('El nombre de la tienda es demasiado largo');
        }

        if ($categoria !== null && $categoria !== '' && strlen($categoria) > 80) {
            return self::ticketValidationError('La categoría es demasiado larga');
        }

        $fecha = null;
        if ($fechaRaw !== '') {
            $fecha = self::parseTicketDate($fechaRaw);
            if ($fecha === null) {
                return self::ticketValidationError('Formato de fecha no válido. Usa dd/mm/aaaa');
            }
        }

        $normalizedProducts = [];
        foreach ($productos as $producto) {
            if (!is_array($producto)) {
                continue;
            }

            $productName = trim((string) ($producto['nombre'] ?? ''));
            if ($productName === '') {
                continue;
            }

            if (strlen($productName) > 120) {
                return self::ticketValidationError('El nombre de un producto es demasiado largo');
            }

            $productPriceRaw = $producto['precio'] ?? null;
            $productPrice = null;

            if ($productPriceRaw !== null && $productPriceRaw !== '') {
                if (!is_numeric($productPriceRaw)) {
                    return self::ticketValidationError('El precio de un producto debe ser numérico');
                }

                $productPrice = (float) $productPriceRaw;

                if ($productPrice < 0) {
                    return self::ticketValidationError('El precio de un producto no puede ser negativo');
                }

                if ($productPrice <= 0) {
                    return self::ticketValidationError('El precio de un producto debe ser mayor que 0');
                }
            }

            $normalizedProducts[] = [
                'nombre' => $productName,
                'precio' => $productPrice,
            ];
        }

        return [
            'error' => null,
            'nombre' => $nombre !== '' ? $nombre : null,
            'precio' => $precio,
            'categoria' => ($categoria === null || $categoria === '') ? null : $categoria,
            'productos' => $normalizedProducts,
            'fecha' => $fecha,
        ];
    }

    private static function parseTicketDate(string $value): ?\DateTimeImmutable
    {
        $value = trim($value);
        if ($value === '') {
            return null;
        }

        $formats = ['d/m/Y', 'd-m-Y', 'Y-m-d', 'Y-m-d H:i:s', 'd/m/Y H:i:s'];

        foreach ($formats as $format) {
            $date = \DateTimeImmutable::createFromFormat($format, $value);
            if ($date instanceof \DateTimeImmutable) {
                $errors = \DateTimeImmutable::getLastErrors();
                if (($errors['warning_count'] ?? 0) === 0 && ($errors['error_count'] ?? 0) === 0) {
                    return $date;
                }
            }
        }

        return null;
    }

    /**
     * @return array{error: ?string, nombre: ?string, precio: ?float, categoria: ?string, productos: array<int, array{nombre: string, precio: ?float}>, fecha: ?\DateTimeInterface}
     */
    private static function ticketValidationError(string $message): array
    {
        return [
            'error' => $message,
            'nombre' => null,
            'precio' => null,
            'categoria' => null,
            'productos' => [],
            'fecha' => null,
        ];
    }
}
