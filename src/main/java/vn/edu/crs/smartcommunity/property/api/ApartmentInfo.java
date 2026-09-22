package vn.edu.crs.smartcommunity.property.api;

public record ApartmentInfo(
        Long id,
        String unitNumber,
        Integer floorNumber,
        Long buildingId,
        String buildingCode,
        String buildingName
) {
}
