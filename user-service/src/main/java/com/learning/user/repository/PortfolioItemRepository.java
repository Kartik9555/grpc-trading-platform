package com.learning.user.repository;

import com.leaning.common.Ticker;
import com.learning.user.entity.PortfolioItem;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioItemRepository extends CrudRepository<PortfolioItem, Long> {
    List<PortfolioItem> findAllByUserId(Long userId);
    Optional<PortfolioItem> findByUserIdAndTicker(Long userId, Ticker ticker);
}
