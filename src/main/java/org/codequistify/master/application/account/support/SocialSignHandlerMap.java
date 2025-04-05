package org.codequistify.master.application.account.support;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.account.service.SocialSignService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SocialSignHandlerMap {

    private final Map<String, SocialSignService> socialSignServiceMap;

    public SocialSignService getHandler(String rawProvider) {
        SocialProvider provider = SocialProvider.from(rawProvider);
        return socialSignServiceMap.get(provider.getKey());
    }
}