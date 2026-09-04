package com.learning.user.service.handler;

import com.leaning.user.UserInformation;
import com.leaning.user.UserInformationRequest;
import com.learning.user.exceptions.UnknownUserException;
import com.learning.user.repository.PortfolioItemRepository;
import com.learning.user.repository.UserRepository;
import com.learning.user.util.EntityMessageMapper;
import org.springframework.stereotype.Service;

@Service
public class UserInformationRequestHandler {

    private final UserRepository userRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    public UserInformationRequestHandler(UserRepository userRepository, PortfolioItemRepository portfolioItemRepository) {
        this.userRepository = userRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }

    public UserInformation getUserInformation(UserInformationRequest request) {
        var userId = (long)request.getUserId();
        var user = this.userRepository.findById(userId)
                .orElseThrow(() -> new UnknownUserException(userId));

        var portfolioItems = this.portfolioItemRepository.findAllByUserId(userId);

        return EntityMessageMapper.toUserInformation(user, portfolioItems);
    }
}
