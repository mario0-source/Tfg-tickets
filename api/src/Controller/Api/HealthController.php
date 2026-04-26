<?php

namespace App\Controller\Api;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class HealthController extends AbstractController
{
    #[Route('/api/health', name: 'app_api_health')]
    public function index(): Response
    {
        return $this->render('api/health/index.html.twig', [
            'controller_name' => 'HealthController',
        ]);
    }
}
