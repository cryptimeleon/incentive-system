package org.cryptimeleon.incentive.client.dto.inc;

import java.util.List;
import java.util.Objects;

public final class TokenUpdateResultsDto {
    private List<ZkpTokenUpdateResultDto> zkpTokenUpdateResultDtoList;
    private List<EarnTokenUpdateResultDto> earnTokenUpdateResultDtoList;

    @SuppressWarnings("unused")
    public TokenUpdateResultsDto() {
    }

    public TokenUpdateResultsDto(final List<ZkpTokenUpdateResultDto> zkpTokenUpdateResultDtoList, final List<EarnTokenUpdateResultDto> earnTokenUpdateResultDtoList) {
        this.zkpTokenUpdateResultDtoList = zkpTokenUpdateResultDtoList;
        this.earnTokenUpdateResultDtoList = earnTokenUpdateResultDtoList;
    }

    public List<ZkpTokenUpdateResultDto> getZkpTokenUpdateResultDtoList() {
        return this.zkpTokenUpdateResultDtoList;
    }

    public List<EarnTokenUpdateResultDto> getEarnTokenUpdateResultDtoList() {
        return this.earnTokenUpdateResultDtoList;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TokenUpdateResultsDto)) return false;
        final TokenUpdateResultsDto other = (TokenUpdateResultsDto) o;
        final Object this$zkpTokenUpdateResultDtoList = this.getZkpTokenUpdateResultDtoList();
        final Object other$zkpTokenUpdateResultDtoList = other.getZkpTokenUpdateResultDtoList();
        if (!Objects.equals(this$zkpTokenUpdateResultDtoList, other$zkpTokenUpdateResultDtoList))
            return false;
        final Object this$earnTokenUpdateResultDtoList = this.getEarnTokenUpdateResultDtoList();
        final Object other$earnTokenUpdateResultDtoList = other.getEarnTokenUpdateResultDtoList();
        return Objects.equals(this$earnTokenUpdateResultDtoList, other$earnTokenUpdateResultDtoList);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $zkpTokenUpdateResultDtoList = this.getZkpTokenUpdateResultDtoList();
        result = result * PRIME + ($zkpTokenUpdateResultDtoList == null ? 43 : $zkpTokenUpdateResultDtoList.hashCode());
        final Object $earnTokenUpdateResultDtoList = this.getEarnTokenUpdateResultDtoList();
        result = result * PRIME + ($earnTokenUpdateResultDtoList == null ? 43 : $earnTokenUpdateResultDtoList.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "TokenUpdateResultsDto(zkpTokenUpdateResultDtoList=" + this.getZkpTokenUpdateResultDtoList() + ", earnTokenUpdateResultDtoList=" + this.getEarnTokenUpdateResultDtoList() + ")";
    }
}
