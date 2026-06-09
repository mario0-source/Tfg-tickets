<?php

namespace App\Controller;

use App\Entity\ProductPriceEntry;
use App\Repository\ProductPriceEntryRepository;
use App\Repository\TicketRepository;
use Doctrine\ORM\EntityManagerInterface;
use OpenApi\Attributes as OA;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[OA\Tag(name: 'Compare', description: 'Comparación de precios entre tiendas')]
final class CompareController extends AbstractController
{
    #[Route('/api/compare/products', methods: ['GET'])]
    #[OA\Get(
        path: '/api/compare/products',
        summary: 'Listar productos comparables',
        responses: [
            new OA\Response(response: 200, description: 'Resumen por producto'),
            new OA\Response(response: 401, description: 'No autenticado'),
        ]
    )]
    public function listProducts(TicketRepository $ticketRepository): JsonResponse
    {
        /** @var \App\Entity\User $user */
        $user = $this->getUser();
        $tickets = $ticketRepository->findBy(['user' => $user]);

        $products = [];

        foreach ($tickets as $ticket) {
            foreach ($ticket->getProductos() ?? [] as $producto) {
                if (!is_array($producto)) {
                    continue;
                }

                $name = trim((string) ($producto['nombre'] ?? ''));
                $price = (float) ($producto['precio'] ?? 0);

                if ($name === '' || $price <= 0) {
                    continue;
                }

                $key = mb_strtolower($name);
                if (!isset($products[$key])) {
                    $products[$key] = [
                        'productName' => $name,
                        'storeCount' => 0,
                        'minPrice' => $price,
                        'maxPrice' => $price,
                        'stores' => [],
                    ];
                }

                $store = (string) $ticket->getNombre();
                $products[$key]['stores'][$store] = true;
                $products[$key]['minPrice'] = min($products[$key]['minPrice'], $price);
                $products[$key]['maxPrice'] = max($products[$key]['maxPrice'], $price);
            }
        }

        $result = array_values(array_map(
            static function (array $item): array {
                $stores = array_keys($item['stores']);

                return [
                    'productName' => $item['productName'],
                    'storeCount' => count($stores),
                    'minPrice' => round($item['minPrice'], 2),
                    'maxPrice' => round($item['maxPrice'], 2),
                    'priceDiff' => round($item['maxPrice'] - $item['minPrice'], 2),
                ];
            },
            $products
        ));

        usort($result, static fn (array $a, array $b): int => strcmp($a['productName'], $b['productName']));

        return $this->json($result);
    }

    #[Route('/api/compare/product/{productName}', methods: ['GET'])]
    #[OA\Get(
        path: '/api/compare/product/{productName}',
        summary: 'Detalle de comparación de un producto',
        parameters: [
            new OA\Parameter(
                name: 'productName',
                in: 'path',
                required: true,
                description: 'Nombre del producto (URL-encoded si lleva espacios)',
                schema: new OA\Schema(type: 'string', example: 'Leche entera')
            ),
        ],
        responses: [
            new OA\Response(response: 200, description: 'Entradas por tienda'),
            new OA\Response(response: 404, description: 'Producto no encontrado'),
            new OA\Response(response: 401, description: 'No autenticado'),
        ]
    )]
    public function productDetail(
        string $productName,
        TicketRepository $ticketRepository,
        ProductPriceEntryRepository $entryRepository
    ): JsonResponse {
        /** @var \App\Entity\User $user */
        $user = $this->getUser();
        $normalizedTarget = mb_strtolower(urldecode($productName));

        $entries = [];

        foreach ($ticketRepository->findBy(['user' => $user]) as $ticket) {
            foreach ($ticket->getProductos() ?? [] as $producto) {
                if (!is_array($producto)) {
                    continue;
                }

                $name = trim((string) ($producto['nombre'] ?? ''));
                $price = (float) ($producto['precio'] ?? 0);

                if ($name === '' || $price <= 0) {
                    continue;
                }

                if (mb_strtolower($name) !== $normalizedTarget) {
                    continue;
                }

                $entries[] = [
                    'store' => (string) $ticket->getNombre(),
                    'price' => round($price, 2),
                    'source' => 'ticket',
                    'date' => $ticket->getFecha()?->format('Y-m-d'),
                ];
            }
        }

        foreach ($entryRepository->findByUserAndProductName($user, urldecode($productName)) as $manual) {
            $entries[] = [
                'store' => $manual->getStore(),
                'price' => round($manual->getPrice() ?? 0.0, 2),
                'source' => 'manual',
                'date' => $manual->getCreatedAt()?->format('Y-m-d'),
            ];
        }

        if ($entries === []) {
            return $this->json(['error' => 'Producto no encontrado'], Response::HTTP_NOT_FOUND);
        }

        usort($entries, static fn (array $a, array $b): int => $a['price'] <=> $b['price']);

        $prices = array_column($entries, 'price');
        $minPrice = min($prices);
        $maxPrice = max($prices);

        return $this->json([
            'productName' => urldecode($productName),
            'entries' => $entries,
            'minPrice' => $minPrice,
            'maxPrice' => $maxPrice,
            'priceDiff' => round($maxPrice - $minPrice, 2),
        ]);
    }

    #[Route('/api/compare/entries', methods: ['POST'])]
    #[OA\Post(
        path: '/api/compare/entries',
        summary: 'Añadir precio manual para comparación',
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(ref: '#/components/schemas/CompareEntryInput')
        ),
        responses: [
            new OA\Response(response: 201, description: 'Precio manual añadido'),
            new OA\Response(response: 400, description: 'Faltan campos obligatorios'),
            new OA\Response(response: 401, description: 'No autenticado'),
        ]
    )]
    public function addManualEntry(
        Request $request,
        EntityManagerInterface $em
    ): JsonResponse {
        $data = json_decode($request->getContent(), true);

        if (!is_array($data) || !isset($data['productName'], $data['store'], $data['price'])) {
            return $this->json(['error' => 'Faltan campos obligatorios'], Response::HTTP_BAD_REQUEST);
        }

        $productName = trim((string) $data['productName']);
        $store = trim((string) $data['store']);

        if ($productName === '' || $store === '') {
            return $this->json(['error' => 'Producto y tienda son obligatorios'], Response::HTTP_BAD_REQUEST);
        }

        if (!is_numeric($data['price'])) {
            return $this->json(['error' => 'El precio debe ser numérico'], Response::HTTP_BAD_REQUEST);
        }

        $price = (float) $data['price'];

        if ($price <= 0) {
            return $this->json(['error' => 'El precio debe ser mayor que 0'], Response::HTTP_BAD_REQUEST);
        }

        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        $entry = new ProductPriceEntry();
        $entry->setUser($user);
        $entry->setProductName($productName);
        $entry->setStore($store);
        $entry->setPrice($price);

        $em->persist($entry);
        $em->flush();

        return $this->json([
            'message' => 'Precio manual añadido',
            'entry' => [
                'store' => $entry->getStore(),
                'price' => round($entry->getPrice() ?? 0.0, 2),
                'source' => 'manual',
                'date' => $entry->getCreatedAt()?->format('Y-m-d'),
            ],
        ], Response::HTTP_CREATED);
    }
}
