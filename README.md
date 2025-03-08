# cp-abe

"/encrypt" endpoint

request body:

```json
{
  "policy": "admin finance hr 2of3",
  "data": "This is my secret message"
}
```

response body:

```json
{
  "encryptedData": "OxG5JwIsKS68AeYHCUx7vI3+0h+FkGbmU0AI59wGJ+8=",
  "message": "Data encrypted successfully",
  "policy": "admin finance hr 2of3"
}
```
