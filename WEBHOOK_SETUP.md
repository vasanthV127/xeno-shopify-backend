# Shopify Webhook Setup Guide

This guide explains how to configure Shopify webhooks for real-time data synchronization with the Xeno platform.

## Overview

Webhooks allow Shopify to send instant notifications to our backend when orders, customers, or products are created or updated, eliminating the need to wait for the scheduled 6-hour sync.

## Webhook Endpoints

The following webhook endpoints are available:

| Event Type | Endpoint URL | Description |
|------------|-------------|-------------|
| Order Create/Update | `https://xeno-shopify-backend-frzt.onrender.com/api/webhooks/shopify/orders/create` | Notified when a new order is created or updated |
| Customer Create/Update | `https://xeno-shopify-backend-frzt.onrender.com/api/webhooks/shopify/customers/create` | Notified when a customer is created or updated |
| Product Create/Update | `https://xeno-shopify-backend-frzt.onrender.com/api/webhooks/shopify/products/create` | Notified when a product is created or updated |

## Security

All webhooks are secured using HMAC-SHA256 signature verification:
- Shopify signs each webhook request with your Shopify Access Token
- Our backend verifies the signature before processing the webhook
- Invalid signatures are rejected with 401 Unauthorized

## Setup Instructions

### Step 1: Access Shopify Admin

1. Log in to your Shopify Admin panel
2. Navigate to **Settings** → **Notifications**
3. Scroll down to the **Webhooks** section

### Step 2: Create Webhook for Orders

1. Click **Create webhook**
2. Configure the webhook:
   - **Event**: `Order creation` or `Order update`
   - **Format**: `JSON`
   - **URL**: `https://xeno-shopify-backend-frzt.onrender.com/api/webhooks/shopify/orders/create`
   - **Webhook API version**: Select latest version (e.g., `2024-10`)
3. Click **Save webhook**

### Step 3: Create Webhook for Customers

1. Click **Create webhook**
2. Configure the webhook:
   - **Event**: `Customer creation` or `Customer update`
   - **Format**: `JSON`
   - **URL**: `https://xeno-shopify-backend-frzt.onrender.com/api/webhooks/shopify/customers/create`
   - **Webhook API version**: Select latest version
3. Click **Save webhook**

### Step 4: Create Webhook for Products

1. Click **Create webhook**
2. Configure the webhook:
   - **Event**: `Product creation` or `Product update`
   - **Format**: `JSON`
   - **URL**: `https://xeno-shopify-backend-frzt.onrender.com/api/webhooks/shopify/products/create`
   - **Webhook API version**: Select latest version
3. Click **Save webhook**

## Testing Webhooks

### Using Shopify Admin

1. Go to **Settings** → **Notifications** → **Webhooks**
2. Click on the webhook you want to test
3. Click **Send test notification**
4. Check the webhook response status (should be `200 OK`)

### Manual Testing with cURL

```bash
# Test order webhook
curl -X POST https://xeno-shopify-backend-frzt.onrender.com/api/webhooks/shopify/orders/create \
  -H "Content-Type: application/json" \
  -H "X-Shopify-Shop-Domain: your-store.myshopify.com" \
  -H "X-Shopify-Hmac-SHA256: <computed-hmac>" \
  -d '{
    "id": "1234567890",
    "order_number": "1001",
    "total_price": "150.00",
    "currency": "USD",
    "customer": {
      "id": "987654321",
      "email": "customer@example.com"
    },
    "created_at": "2025-12-04T10:00:00Z"
  }'
```

## Monitoring Webhooks

### Check Webhook Logs in Shopify

1. Go to **Settings** → **Notifications** → **Webhooks**
2. Click on any webhook to view its delivery history
3. Check for:
   - **Success rate**: Should be close to 100%
   - **Response times**: Should be under 5 seconds
   - **Failed deliveries**: Investigate and resolve any failures

### Check Application Logs

Monitor backend logs on Render:
1. Go to Render Dashboard
2. Select `xeno-shopify-backend` service
3. Click **Logs** tab
4. Search for:
   - `Received order create webhook` - Successful webhook receipt
   - `Order {} saved successfully` - Successful processing
   - `Invalid webhook signature` - Authentication failures
   - `Error processing order webhook` - Processing errors

## Troubleshooting

### Webhook Returns 401 Unauthorized

**Cause**: HMAC signature verification failed

**Solutions**:
- Verify that the Shopify Access Token in your Xeno account settings is correct
- Ensure the webhook is created under the same Shopify store
- Check that the shop domain matches your Xeno tenant's Shopify domain

### Webhook Returns 404 Not Found

**Cause**: No tenant found for the shop domain

**Solutions**:
- Verify that your Shopify domain in Xeno matches the shop sending webhooks
- Check the `X-Shopify-Shop-Domain` header value
- Ensure the tenant exists in the database

### Webhook Returns 500 Internal Server Error

**Cause**: Server error during processing

**Solutions**:
- Check backend logs on Render for detailed error messages
- Verify database connectivity
- Ensure all required fields are present in the webhook payload

### Webhooks Not Triggering

**Causes & Solutions**:
- **Webhook not created**: Follow setup instructions above
- **Webhook disabled**: Re-enable in Shopify Admin
- **Backend service down**: Check Render service status
- **Network issues**: Verify backend URL is accessible

## Webhook Payload Examples

### Order Webhook Payload

```json
{
  "id": 5678901234,
  "order_number": "1234",
  "email": "customer@example.com",
  "created_at": "2025-12-04T10:30:00Z",
  "total_price": "299.99",
  "subtotal_price": "279.99",
  "total_tax": "20.00",
  "total_shipping": "0.00",
  "currency": "USD",
  "financial_status": "paid",
  "fulfillment_status": "fulfilled",
  "customer": {
    "id": 1234567890,
    "email": "customer@example.com",
    "first_name": "John",
    "last_name": "Doe"
  }
}
```

### Customer Webhook Payload

```json
{
  "id": 1234567890,
  "email": "customer@example.com",
  "first_name": "John",
  "last_name": "Doe",
  "phone": "+1234567890",
  "orders_count": 5,
  "total_spent": "1499.95",
  "created_at": "2025-01-15T08:00:00Z"
}
```

### Product Webhook Payload

```json
{
  "id": 9876543210,
  "title": "Premium T-Shirt",
  "vendor": "Acme Clothing",
  "product_type": "Apparel",
  "status": "active",
  "created_at": "2025-02-01T12:00:00Z",
  "variants": [
    {
      "id": 1111111111,
      "price": "29.99",
      "sku": "TSHIRT-001"
    }
  ]
}
```

## Best Practices

1. **Monitor webhook delivery rates**: Aim for 95%+ success rate
2. **Set up retry logic**: Shopify automatically retries failed webhooks
3. **Keep responses fast**: Webhook handlers should respond within 5 seconds
4. **Log all webhook events**: For debugging and audit purposes
5. **Validate payload structure**: Handle missing or malformed fields gracefully
6. **Use idempotency**: Handle duplicate webhooks (Shopify may send duplicates)

## Additional Resources

- [Shopify Webhook Documentation](https://shopify.dev/docs/api/admin-rest/2024-10/resources/webhook)
- [HMAC Verification Guide](https://shopify.dev/docs/apps/webhooks/configuration/https#step-5-verify-the-webhook)
- [Webhook Best Practices](https://shopify.dev/docs/apps/webhooks/best-practices)

## Support

For webhook-related issues:
1. Check this documentation
2. Review backend logs on Render
3. Test webhooks using Shopify's test notification feature
4. Verify HMAC signature calculation
