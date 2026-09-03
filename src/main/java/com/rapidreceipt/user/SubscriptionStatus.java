package com.rapidreceipt.user;

/**
 * Represents the current status of a user's subscription.
 * TRIAL    — new user within the free trial window.
 * ACTIVE   — subscription is paid and active.
 * INACTIVE — subscription expired or was cancelled.
 */
public enum SubscriptionStatus {
    TRIAL,
    ACTIVE,
    INACTIVE
}
