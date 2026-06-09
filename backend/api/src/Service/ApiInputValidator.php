<?php

namespace App\Service;

final class ApiInputValidator
{
    public static function validateEmail(string $email): ?string
    {
        $email = trim($email);

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

    public static function validatePassword(string $password): ?string
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

    /**
     * @param array<string, mixed> $data
     * @return array{error: ?string, nombre: ?string, precio: ?float, categoria: ?string, productos: array<int, array{nombre: string, precio: ?float}>, fecha: ?\DateTimeInterface}
     */
    public static function validateTicketPayload(array $data, bool $requireAll = true): array
    {
        $nombre = isset($data['nombre']) ? trim((string) $data['nombre']) : '';
        $precioRaw = $data['precio'] ?? null;
        $categoria = isset($data['categoria']) ? trim((string) $data['categoria']) : null;
        $productos = is_array($data['productos'] ?? null) ? $data['productos'] : [];
        $fechaRaw = isset($data['fecha']) ? trim((string) $data['fecha']) : '';

        if ($requireAll && $nombre === '') {
            return self::ticketError('El nombre de la tienda es obligatorio');
        }

        if ($requireAll && ($precioRaw === null || $precioRaw === '')) {
            return self::ticketError('El precio es obligatorio');
        }

        if ($precioRaw !== null && $precioRaw !== '' && !is_numeric($precioRaw)) {
            return self::ticketError('El precio debe ser un número válido');
        }

        $precio = ($precioRaw === null || $precioRaw === '') ? null : (float) $precioRaw;

        if ($precio !== null && $precio <= 0) {
            return self::ticketError('El precio debe ser mayor que 0');
        }

        if ($precio !== null && $precio > 999999.99) {
            return self::ticketError('El precio es demasiado alto');
        }

        if ($nombre !== '' && strlen($nombre) > 120) {
            return self::ticketError('El nombre de la tienda es demasiado largo');
        }

        if ($categoria !== null && $categoria !== '' && strlen($categoria) > 80) {
            return self::ticketError('La categoría es demasiado larga');
        }

        $fecha = null;
        if ($fechaRaw !== '') {
            $fecha = self::parseDate($fechaRaw);
            if ($fecha === null) {
                return self::ticketError('Formato de fecha no válido. Usa dd/mm/aaaa');
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
                return self::ticketError('El nombre de un producto es demasiado largo');
            }

            $productPriceRaw = $producto['precio'] ?? null;
            $productPrice = null;

            if ($productPriceRaw !== null && $productPriceRaw !== '') {
                if (!is_numeric($productPriceRaw)) {
                    return self::ticketError('El precio de un producto debe ser numérico');
                }

                $productPrice = (float) $productPriceRaw;

                if ($productPrice < 0) {
                    return self::ticketError('El precio de un producto no puede ser negativo');
                }

                if ($productPrice <= 0) {
                    return self::ticketError('El precio de un producto debe ser mayor que 0');
                }
            }

            $normalizedProducts[] = [
                'nombre' => $productName,
                'precio' => $productPrice,
            ];
        }

        if ($requireAll && $normalizedProducts === [] && $precio !== null) {
            return self::ticketError('Debe haber al menos un producto con nombre');
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

    public static function parseDate(string $value): ?\DateTimeImmutable
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
    private static function ticketError(string $message): array
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
