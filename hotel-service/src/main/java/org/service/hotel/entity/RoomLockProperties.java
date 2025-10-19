package org.service.hotel.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "room.lock")
@Data
public class RoomLockProperties {
    private long timeoutSeconds = 30;
}
