package de.upb.crypto.incentive.procotols.issue;

import java.util.Objects;

public class IssueResponse {
    final int id;
    final String payload;

    public IssueResponse(int id, String payload) {
        this.id = id;
        this.payload = payload;
    }

    public int getId() {
        return id;
    }

    public String getPayload() {
        return payload;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IssueResponse that = (IssueResponse) o;
        return id == that.id &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, payload);
    }
}