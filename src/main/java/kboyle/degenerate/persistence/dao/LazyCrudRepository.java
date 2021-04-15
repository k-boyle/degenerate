package kboyle.degenerate.persistence.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface LazyCrudRepository<ENTITY, ID> extends CrudRepository<ENTITY, ID> {
    default ENTITY get(ID id) {
        return findById(id).orElseGet(() -> save(createNew(id)));
    }

    ENTITY createNew(ID id);
}
