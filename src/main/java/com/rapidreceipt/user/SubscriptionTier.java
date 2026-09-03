package com.rapidreceipt.user;

/**
 * Represents the subscription tier a user is on.
 * FREE   — default, limited invoice count per month.
 * BASIC  — mid-tier, higher limits.
 * PRO    — unlimited, full feature access.
 */
public enum SubscriptionTier {
    FREE,
    BASIC,
    PRO
}
