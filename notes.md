## Design notes

### Server process-event signature

I toyed with the idea of having the same process-event signature on the server side
with a server state which the processing would return a new version.

That is a good thing on the client (single user, single thread) but would hinder performance
on the server as it would serialize the process-event calls and there may be thousands of
clients.

The server provides a `context` parameter which is a user defined map (per client connection)
and it may contain handles to external things that are required (like database connections).

The client info is also a map for future extensibility.