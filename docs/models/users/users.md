# 회원 모델

## 회원
```mermaid
classDiagram
    direction LR

    class User {
        <<AggregateRoot>>
        id: String
        email: String 
        name: String
        gender: Integer
        birthDate: String
        phone: String
        profileImageUrl: String
        createdAt: Timestamp
        updatedAt: Timestamp
        deletedAt: Timestamp
    }
```
