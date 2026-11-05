package com.acme.coldtrace.platform.aiassistance.application.commandservices;

/**
 * Failure types for AI assistance use cases.
 *
 * @since 1.0
 */
public sealed interface AiAssistanceFailure permits
        AiAssistanceFailure.ProviderDisabled,
        AiAssistanceFailure.UnsupportedProvider,
        AiAssistanceFailure.ProviderNotConfigured,
        AiAssistanceFailure.ProviderUnavailable,
        AiAssistanceFailure.ProviderTimeout,
        AiAssistanceFailure.InvalidStructuredOutput {
    /** @return message key to resolve through i18n */
    String messageKey();

    /** @return optional message interpolation arguments */
    default Object[] args() {
        return new Object[0];
    }

    /** AI assistance is disabled by configuration. */
    record ProviderDisabled() implements AiAssistanceFailure {
        @Override
        public String messageKey() {
            return "ai-assistance.error.provider-disabled";
        }
    }

    /** Configured provider is not supported by this backend. */
    record UnsupportedProvider(String provider) implements AiAssistanceFailure {
        @Override
        public String messageKey() {
            return "ai-assistance.error.unsupported-provider";
        }

        @Override
        public Object[] args() {
            return new Object[]{provider};
        }
    }

    /** No Spring AI chat client is available for the configured provider. */
    record ProviderNotConfigured(String provider) implements AiAssistanceFailure {
        @Override
        public String messageKey() {
            return "ai-assistance.error.provider-not-configured";
        }

        @Override
        public Object[] args() {
            return new Object[]{provider};
        }
    }

    /** Provider request failed before returning structured content. */
    record ProviderUnavailable() implements AiAssistanceFailure {
        @Override
        public String messageKey() {
            return "ai-assistance.error.provider-unavailable";
        }
    }

    /** Provider did not return a response before the configured timeout. */
    record ProviderTimeout() implements AiAssistanceFailure {
        @Override
        public String messageKey() {
            return "ai-assistance.error.provider-timeout";
        }
    }

    /** Provider returned content that could not be converted or validated. */
    record InvalidStructuredOutput(String details) implements AiAssistanceFailure {
        @Override
        public String messageKey() {
            return "ai-assistance.error.invalid-structured-output";
        }

        @Override
        public Object[] args() {
            return new Object[]{details};
        }
    }
}
