package mappers.interfaces;

import dto.base.BaseDto;

public interface Mapper<D extends BaseDto, T> {
    T dtoToEntity(D dto);
}
