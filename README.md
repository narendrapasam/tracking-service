
# Tracking Number Generation Service

This module provides a reliable and deterministic way to generate unique tracking numbers for shipments using a combination of customer and shipment details, along with SHA-256 hashing.

## How It Works

The tracking number is derived by securely hashing a unique combination of the following fields:

- origin (city/location of dispatch)
- destination (city/location of delivery)
- weight` (with 3 decimal precision)
- customerId (UUID)
- customerName
- customerSlug
- A randomly generated 6-character secure code

In high-concurrency environments, repeatedly creating objects like `MessageDigest` can lead to performance bottlenecks and race conditions. To address this, the service uses `ThreadLocal<MessageDigest>` to ensure that:

- Each thread has its **own isolated instance** of `MessageDigest`
- There is **no shared mutable state** across threads
- Performance is improved by **reusing** the same object within a thread

In this service, `ThreadLocalRandom` is ideal for generating random secure codes (e.g., for tracking number suffixes) in a **concurrent and scalable** environment without performance penalties

The fields are joined together using the pipe `|` character, converted to a `SHA-256` hash, and the first 16 characters of the resulting hexadecimal string are used as the tracking number.

---

## Example

### Input
origin = "IN"
destination = "US"
weight = 12.345
createdAt = ZonedDateTime.now()
customerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
customerName = "Test User"
customerSlug = "test-user"


## API Links

**Swagger UI:**  
**(http://localhost:8081/tracking/swagger-ui/index.html)**

**Sample Request URL:**  
**(http://localhost:8081/tracking/next-tracking-number?origin_country_id=US&destination_country_id=IN&weight=999.99&created_at=2025-06-11T20%3A45%3A00%2B08%3A00&customer_id=de619854-b59b-425e-9db4-943979e1bd49&customer_name=RedBox%20Logistics&customer_slug=redbox-logistics)**



