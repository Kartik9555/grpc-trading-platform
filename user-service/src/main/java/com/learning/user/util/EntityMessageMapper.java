package com.learning.user.util;

import com.leaning.user.Holding;
import com.leaning.user.UserInformation;
import com.learning.user.entity.PortfolioItem;
import com.learning.user.entity.User;

import java.util.List;

public class EntityMessageMapper {

    public static UserInformation toUserInformation(User user, List<PortfolioItem> items) {
        var holdings = items.stream()
                .map(i -> Holding.newBuilder()
                        .setTicker(i.getTicker())
                        .setQuantity(i.getQuantity())
                        .build())
                .toList();
        return UserInformation.newBuilder()
                .setName(user.getName())
                .setBalance(user.getBalance())
                .setUserId(user.getId().intValue())
                .addAllHoldings(holdings)
                .build();
    }
}
