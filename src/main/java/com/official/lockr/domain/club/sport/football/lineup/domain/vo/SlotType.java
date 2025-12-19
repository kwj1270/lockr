package com.official.lockr.domain.club.sport.football.lineup.domain.vo;

/**
     * 슬롯 타입 (선발 or 후보)
     */
    public enum SlotType {
        /**
         * 선발 (11명, 인덱스 0~10)
         */
        STARTER("starter"),

        /**
         * 후보 (7명, 인덱스 0~6)
         */
        SUBSTITUTE("substitute");

        private final String value;

        SlotType(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static SlotType fromValue(final String value) {
            for (SlotType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid slot type: " + value);
        }
    }
