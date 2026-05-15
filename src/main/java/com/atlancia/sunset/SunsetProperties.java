package com.atlancia.sunset;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.api-sunset")
public class SunsetProperties {

    private boolean enabled = true;
    private SunsetAction onSunset = SunsetAction.RETURN_410;
    private String consumerHeader = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public SunsetAction getOnSunset() {
        return onSunset;
    }

    public void setOnSunset(SunsetAction onSunset) {
        this.onSunset = onSunset;
    }

    public String getConsumerHeader() {
        return consumerHeader;
    }

    public void setConsumerHeader(String consumerHeader) {
        this.consumerHeader = consumerHeader;
    }
}
