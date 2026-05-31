<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260523120000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add product_price_entry table for manual compare entries';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('CREATE TABLE product_price_entry (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, product_name VARCHAR(150) NOT NULL, store VARCHAR(100) NOT NULL, price DOUBLE PRECISION NOT NULL, created_at DATETIME NOT NULL, INDEX IDX_PPE_USER (user_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE product_price_entry ADD CONSTRAINT FK_PPE_USER FOREIGN KEY (user_id) REFERENCES user (id)');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE product_price_entry DROP FOREIGN KEY FK_PPE_USER');
        $this->addSql('DROP TABLE product_price_entry');
    }
}
