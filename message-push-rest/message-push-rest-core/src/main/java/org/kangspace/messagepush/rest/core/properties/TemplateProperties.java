package org.kangspace.messagepush.rest.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 配置属性
 *
 * @author kango2gler@gmail.com
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "template")
public class TemplateProperties {
}
