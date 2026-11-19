package com.example.projeto_padrao.repository;

import com.example.projeto_padrao.domain.TabelaGeralItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TabelaGeralItemRepository extends JpaRepository<TabelaGeralItem, Long> {

    List<TabelaGeralItem> findAllByOrderByOrdemAsc();
}
