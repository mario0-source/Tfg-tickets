<?php

namespace App\Command;

use App\Entity\ProductPriceEntry;
use App\Entity\Ticket;
use App\Entity\User;
use App\Repository\ProductPriceEntryRepository;
use App\Repository\TicketRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

#[AsCommand(
    name: 'app:load-demo-data',
    description: 'Carga usuario y tickets de demostración para la defensa del TFG'
)]
class LoadDemoDataCommand extends Command
{
    private const DEFAULT_EMAIL = 'demo@nebulatickets.es';
    private const DEFAULT_PASSWORD = 'Demo2025!';

    public function __construct(
        private readonly EntityManagerInterface $em,
        private readonly UserRepository $userRepository,
        private readonly TicketRepository $ticketRepository,
        private readonly ProductPriceEntryRepository $priceEntryRepository,
        private readonly UserPasswordHasherInterface $passwordHasher,
    ) {
        parent::__construct();
    }

    protected function configure(): void
    {
        $this
            ->addOption('email', null, InputOption::VALUE_OPTIONAL, 'Email del usuario demo', self::DEFAULT_EMAIL)
            ->addOption('password', null, InputOption::VALUE_OPTIONAL, 'Contraseña del usuario demo', self::DEFAULT_PASSWORD)
            ->addOption('reset', null, InputOption::VALUE_NONE, 'Borra tickets y precios manuales previos del usuario demo antes de cargar');
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);
        $email = (string) $input->getOption('email');
        $password = (string) $input->getOption('password');
        $reset = (bool) $input->getOption('reset');

        $user = $this->userRepository->findOneBy(['email' => $email]);

        if ($user === null) {
            $user = new User();
            $user->setEmail($email);
            $user->setRoles(['ROLE_USER']);
            $user->setPassword($this->passwordHasher->hashPassword($user, $password));
            $this->em->persist($user);
            $io->writeln('Usuario demo creado.');
        } else {
            $user->setPassword($this->passwordHasher->hashPassword($user, $password));
            $io->writeln('Usuario demo ya existía; contraseña actualizada.');
        }

        if ($reset) {
            $this->purgeUserData($user);
            $io->writeln('Datos anteriores del usuario demo eliminados.');
        }

        $existingTickets = $this->ticketRepository->count(['user' => $user]);
        if ($existingTickets > 0 && !$reset) {
            $io->warning('El usuario demo ya tiene tickets. Ejecuta con --reset para recargar desde cero.');

            return Command::SUCCESS;
        }

        foreach ($this->demoTickets() as $ticketData) {
            $ticket = new Ticket();
            $ticket->setUser($user);
            $ticket->setNombre($ticketData['tienda']);
            $ticket->setPrecio($ticketData['total']);
            $ticket->setCategoria($ticketData['categoria']);
            $ticket->setFecha(new \DateTime($ticketData['fecha']));
            $ticket->setProductos($ticketData['productos']);
            $this->em->persist($ticket);
        }

        foreach ($this->demoManualPrices() as $manual) {
            $entry = new ProductPriceEntry();
            $entry->setUser($user);
            $entry->setProductName($manual['producto']);
            $entry->setStore($manual['tienda']);
            $entry->setPrice($manual['precio']);
            $entry->setCreatedAt(new \DateTime($manual['fecha']));
            $this->em->persist($entry);
        }

        $this->em->flush();

        $io->success('Datos de demostración cargados correctamente.');
        $io->section('Credenciales para la defensa');
        $io->listing([
            'Email: '.$email,
            'Contraseña: '.$password,
            'Login API: POST /api/login_check',
            'Swagger: /api/doc',
        ]);
        $io->section('Qué incluye la demo');
        $io->listing([
            '6 tickets en Mercadona, Lidl, Carrefour y Dia (mayo y junio 2026)',
            'Productos repetidos entre tiendas para probar Comparar (leche, pan, aceite…)',
            '2 precios manuales extra en Comparar',
            'Estadísticas mensuales con variación entre mayo y junio',
        ]);

        return Command::SUCCESS;
    }

    private function purgeUserData(User $user): void
    {
        foreach ($this->ticketRepository->findBy(['user' => $user]) as $ticket) {
            $this->em->remove($ticket);
        }

        foreach ($this->priceEntryRepository->findBy(['user' => $user]) as $entry) {
            $this->em->remove($entry);
        }

        $this->em->flush();
    }

    /**
     * @return list<array{tienda: string, total: float, categoria: string, fecha: string, productos: list<array{nombre: string, precio: float}>}>
     */
    private function demoTickets(): array
    {
        return [
            [
                'tienda' => 'Mercadona',
                'total' => 42.30,
                'categoria' => 'Alimentación',
                'fecha' => '2026-05-08',
                'productos' => [
                    ['nombre' => 'Leche entera 1L', 'precio' => 0.85],
                    ['nombre' => 'Pan de molde', 'precio' => 1.10],
                    ['nombre' => 'Aceite de oliva 1L', 'precio' => 4.25],
                    ['nombre' => 'Yogur natural pack 8', 'precio' => 1.89],
                    ['nombre' => 'Arroz largo 1kg', 'precio' => 1.15],
                    ['nombre' => 'Tomate frito pack 3', 'precio' => 1.95],
                    ['nombre' => 'Pechuga de pollo 1kg', 'precio' => 6.80],
                    ['nombre' => 'Plátano 1kg', 'precio' => 1.45],
                    ['nombre' => 'Café molido 250g', 'precio' => 2.65],
                    ['nombre' => 'Huevos docena M', 'precio' => 2.45],
                    ['nombre' => 'Galletas María', 'precio' => 1.20],
                    ['nombre' => 'Agua mineral pack 6', 'precio' => 1.56],
                ],
            ],
            [
                'tienda' => 'Lidl',
                'total' => 35.60,
                'categoria' => 'Alimentación',
                'fecha' => '2026-05-15',
                'productos' => [
                    ['nombre' => 'Leche entera 1L', 'precio' => 0.79],
                    ['nombre' => 'Pan de molde', 'precio' => 0.95],
                    ['nombre' => 'Aceite de oliva 1L', 'precio' => 3.99],
                    ['nombre' => 'Yogur natural pack 8', 'precio' => 1.65],
                    ['nombre' => 'Pasta espagueti 500g', 'precio' => 0.75],
                    ['nombre' => 'Atún en aceite pack 6', 'precio' => 5.40],
                    ['nombre' => 'Zumo naranja 1L', 'precio' => 1.45],
                    ['nombre' => 'Coca-Cola 2L', 'precio' => 1.89],
                    ['nombre' => 'Cerveza lata pack 6', 'precio' => 3.29],
                    ['nombre' => 'Queso rallado 200g', 'precio' => 1.99],
                    ['nombre' => 'Papel aluminio', 'precio' => 1.89],
                    ['nombre' => 'Servilletas pack 100', 'precio' => 1.55],
                ],
            ],
            [
                'tienda' => 'Carrefour',
                'total' => 38.90,
                'categoria' => 'Alimentación',
                'fecha' => '2026-05-22',
                'productos' => [
                    ['nombre' => 'Leche entera 1L', 'precio' => 0.92],
                    ['nombre' => 'Pan de molde', 'precio' => 1.25],
                    ['nombre' => 'Aceite de oliva 1L', 'precio' => 4.49],
                    ['nombre' => 'Huevos docena M', 'precio' => 2.89],
                    ['nombre' => 'Detergente ropa 1.5L', 'precio' => 3.15],
                    ['nombre' => 'Pechuga de pollo 1kg', 'precio' => 7.20],
                    ['nombre' => 'Café molido 250g', 'precio' => 2.89],
                    ['nombre' => 'Galletas María', 'precio' => 1.35],
                    ['nombre' => 'Refresco limón 2L', 'precio' => 1.69],
                    ['nombre' => 'Jamón cocido 250g', 'precio' => 2.45],
                    ['nombre' => 'Bolsa basura 30L', 'precio' => 2.10],
                    ['nombre' => 'Sal fina 1kg', 'precio' => 0.65],
                ],
            ],
            [
                'tienda' => 'Mercadona',
                'total' => 18.45,
                'categoria' => 'Hogar',
                'fecha' => '2026-06-01',
                'productos' => [
                    ['nombre' => 'Detergente ropa 1.5L', 'precio' => 2.89],
                    ['nombre' => 'Papel higiénico 12 rollos', 'precio' => 4.50],
                    ['nombre' => 'Lejía 2L', 'precio' => 1.25],
                    ['nombre' => 'Estropajo pack 3', 'precio' => 1.10],
                    ['nombre' => 'Ambientador spray', 'precio' => 2.35],
                    ['nombre' => 'Bolsas congelación', 'precio' => 1.89],
                    ['nombre' => 'Bayeta microfibra', 'precio' => 1.49],
                    ['nombre' => 'Guantes limpieza', 'precio' => 1.98],
                ],
            ],
            [
                'tienda' => 'Dia',
                'total' => 15.30,
                'categoria' => 'Alimentación',
                'fecha' => '2026-06-02',
                'productos' => [
                    ['nombre' => 'Leche entera 1L', 'precio' => 0.88],
                    ['nombre' => 'Café molido 250g', 'precio' => 2.45],
                    ['nombre' => 'Galletas María', 'precio' => 1.20],
                    ['nombre' => 'Chocolate con leche', 'precio' => 1.15],
                    ['nombre' => 'Galletas digestive', 'precio' => 1.35],
                    ['nombre' => 'Nata para cocinar', 'precio' => 1.05],
                    ['nombre' => 'Caldo pollo brick', 'precio' => 0.95],
                    ['nombre' => 'Maíz dulce lata', 'precio' => 0.89],
                    ['nombre' => 'Salsa tomate 400g', 'precio' => 0.95],
                    ['nombre' => 'Frutos secos 150g', 'precio' => 2.43],
                ],
            ],
            [
                'tienda' => 'Lidl',
                'total' => 12.80,
                'categoria' => 'Bebidas',
                'fecha' => '2026-06-03',
                'productos' => [
                    ['nombre' => 'Coca-Cola 2L', 'precio' => 1.89],
                    ['nombre' => 'Agua mineral pack 6', 'precio' => 1.65],
                    ['nombre' => 'Zumo naranja 1L', 'precio' => 1.45],
                    ['nombre' => 'Nestea lata pack 6', 'precio' => 3.29],
                    ['nombre' => 'Cerveza lata pack 6', 'precio' => 3.29],
                    ['nombre' => 'Vino tinto botella', 'precio' => 1.23],
                ],
            ],
        ];
    }

    /**
     * @return list<array{producto: string, tienda: string, precio: float, fecha: string}>
     */
    private function demoManualPrices(): array
    {
        return [
            [
                'producto' => 'Coca-Cola 2L',
                'tienda' => 'Mercadona',
                'precio' => 1.95,
                'fecha' => '2026-06-02',
            ],
            [
                'producto' => 'Detergente ropa 1.5L',
                'tienda' => 'Dia',
                'precio' => 2.75,
                'fecha' => '2026-06-03',
            ],
        ];
    }
}
