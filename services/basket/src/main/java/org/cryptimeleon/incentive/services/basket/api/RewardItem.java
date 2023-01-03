package org.cryptimeleon.incentive.services.basket.api;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Dataclass representing dumb coupons / reward items added by the incentive service after successful spend-deduct.
 */
public class RewardItem {
    @ApiModelProperty("${item.id}")
    String id;
    @ApiModelProperty("${item.title}")
    String title;

    public RewardItem(final String id, final String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RewardItem)) return false;
        final RewardItem other = (RewardItem) o;
        if (!other.canEqual(this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (!Objects.equals(this$id, other$id)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        return Objects.equals(this$title, other$title);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RewardItem;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "RewardItem(id=" + this.getId() + ", title=" + this.getTitle() + ")";
    }
}
