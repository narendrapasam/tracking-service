
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

In this service, `ThreadLocalRandom` is ideal for generating random secure codes in a **concurrent and scalable** environment without performance penalties

The fields are joined together using the pipe `|` character, converted to a `SHA-256` hash, and the first 16 characters of the resulting hexadecimal string are used as the tracking number.

## 🔧 Additional Uniqueness Enhancements (New Logic)

To further ensure **global uniqueness and high concurrency support** without using a centralized database, the tracking number generation logic now incorporates:

### 1. IP-Based Node Identification

- A **single character** is derived from the **last byte of the system's IP address**, encoded as a base-36 uppercase character.
- This helps differentiate numbers generated across multiple hosts/nodes.
- If the IP address is unavailable, a fallback random base-36 character is used.

### 2. Time + Random Entropy Segment

- A **custom epoch timestamp** is combined with a **74-bit random number**, ensuring:
  - Strict uniqueness even in millisecond-level concurrent executions.
  - Encoding into **base-36** ensures compactness within the fixed 16-character limit.

### 3. Structure of Final Tracking Number

- The final format is always 16 characters:
  - `PREFIX(3 chars)` from SHA-256 of business data.
  - `NODE(1 char)` from IP-derived token.
  - `SUFFIX(12 chars)` from timestamp and random entropy (base-36).

This approach provides **99.999999%+ uniqueness guarantees** in high-concurrency, distributed environments without relying on any database or coordination service. It preserves a fixed 16-character format while ensuring high entropy through a combination of SHA-256 hashing, node-level differentiation, timestamp encoding, and cryptographically strong randomness.



---

## Example

### Input
origin_country_id = "US";
destination_country_id = "IN";
weight = 999.99;
created_at = ZonedDateTime.parse("2025-06-11T20:45:00+08:00");
customer_id = UUID.fromString("de619854-b59b-425e-9db4-943979e1bd49");
customer_name = "RedBox Logistics";
customer_slug = "redbox-logistics";

## API Links

**Swagger UI:**  
**(http://localhost:8081/tracking/swagger-ui/index.html)**

**Sample Request URL:**  
**(http://localhost:8081/tracking/next-tracking-number?origin_country_id=US&destination_country_id=IN&weight=999.99&created_at=2025-06-11T20%3A45%3A00%2B08%3A00&customer_id=de619854-b59b-425e-9db4-943979e1bd49&customer_name=RedBox%20Logistics&customer_slug=redbox-logistics)**



