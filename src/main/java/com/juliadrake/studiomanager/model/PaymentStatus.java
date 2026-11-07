package com.juliadrake.studiomanager.model;

/**
 * Payment lifecycle states. This build only supports the demo checkout,
 * so the single state records that a retainer was "paid" in demo mode —
 * no real money ever moves.
 */
public enum PaymentStatus {
    PAID_DEMO
}
