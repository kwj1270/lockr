# 인증 모델

## 인증
```mermaid
classDiagram
    direction LR

    class SignUpAuthorization {
        <<AggregateRoot>>
        id: String
        userId: String
        providerId: String
        providerType: ProviderType
        providerAdditionalInfo: Json
        createdAt: TimeStamp
        updatedAt: TimeStamp
        deletedAt: Timestamp
    }

    class ProviderType {
        <<enumeration>>
        GOOGLE
        APPLE
        TEST
    }

    class SignInHistory {
        id: Long
        userId: String
        providerType: ProviderType
        ipAddress: String
        userAgent: String
        deviceInfo: String
        createdAt: Timestamp
    }

    class UserSession {
        id: String
        userId: String
        deviceId: String 
        deviceInfo: String 
        ipAddress: String 
        expiresAt: Timestamp
        createdAt: Timestamp
        updatedAt: Timestamp
        deletedAt: Timestamp
    }
```
