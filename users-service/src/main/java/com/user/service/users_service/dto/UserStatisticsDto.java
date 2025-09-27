package com.user.service.users_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStatisticsDto {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long verifiedUsers;
    private long unverifiedUsers;
    private long lockedUsers;
    private long usersCreatedToday;
    private long usersCreatedThisWeek;
    private long usersCreatedThisMonth;

}
