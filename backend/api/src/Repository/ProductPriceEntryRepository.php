<?php

namespace App\Repository;

use App\Entity\ProductPriceEntry;
use App\Entity\User;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<ProductPriceEntry>
 */
class ProductPriceEntryRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, ProductPriceEntry::class);
    }

    /**
     * @return ProductPriceEntry[]
     */
    public function findByUserAndProductName(User $user, string $productName): array
    {
        return $this->createQueryBuilder('p')
            ->andWhere('p.user = :user')
            ->andWhere('LOWER(p.productName) = LOWER(:productName)')
            ->setParameter('user', $user)
            ->setParameter('productName', $productName)
            ->orderBy('p.createdAt', 'DESC')
            ->getQuery()
            ->getResult();
    }
}
