package com.learning.user.service.handler;

import com.leaning.common.Ticker;
import com.leaning.user.StockTradeRequest;
import com.leaning.user.StockTradeResponse;
import com.learning.user.exceptions.InsufficientBalanceException;
import com.learning.user.exceptions.InsufficientSharesException;
import com.learning.user.exceptions.UnknownTickerException;
import com.learning.user.exceptions.UnknownUserException;
import com.learning.user.repository.PortfolioItemRepository;
import com.learning.user.repository.UserRepository;
import com.learning.user.util.EntityMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockTradeRequestHandler {

    private final UserRepository userRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    public StockTradeRequestHandler(UserRepository userRepository, PortfolioItemRepository portfolioItemRepository) {
        this.userRepository = userRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }

    @Transactional
    public StockTradeResponse buyStock(StockTradeRequest request) {
        // validate
        this.validateTicker(request.getTicker());
        var userId = (long)request.getUserId();
        var user = this.userRepository.findById(userId)
                .orElseThrow(() -> new UnknownUserException(userId));

        var totalPrice = request.getPrice() * request.getQuantity();
        this.validateUserBalance(userId, user.getBalance(), totalPrice);

        // update
        user.setBalance(user.getBalance() - totalPrice);
        this.portfolioItemRepository.findByUserIdAndTicker(userId, request.getTicker())
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.getQuantity()),
                        () -> this.portfolioItemRepository.save(EntityMessageMapper.toPortfolioItem(request))
                );

        return EntityMessageMapper.toStockTradeResponse(request, user.getBalance());
    }

    @Transactional
    public StockTradeResponse sellStock(StockTradeRequest request) {
        // validate
        this.validateTicker(request.getTicker());
        var userId = (long)request.getUserId();
        var user = this.userRepository.findById(userId)
                .orElseThrow(() -> new UnknownUserException(userId));
        var portfolioItem = this.portfolioItemRepository.findByUserIdAndTicker(userId, request.getTicker())
                .filter(pi -> pi.getQuantity() >= request.getQuantity())
                .orElseThrow(() -> new InsufficientSharesException(userId));

        // valid request
        var totalPrice = request.getPrice() * request.getQuantity();
        user.setBalance(user.getBalance() + totalPrice);
        portfolioItem.setQuantity(portfolioItem.getQuantity() - request.getQuantity());
        return EntityMessageMapper.toStockTradeResponse(request, user.getBalance());
    }

    private void validateTicker(com.leaning.common.Ticker ticker) {
        if(Ticker.UNKNOWN.equals(ticker)) {
            throw new UnknownTickerException();
        }
    }

    private void validateUserBalance(Long userId, Integer userBalance, Integer totalPrice) {
        if(totalPrice > userBalance) {
            throw new InsufficientBalanceException(userId);
        }
    }
}
