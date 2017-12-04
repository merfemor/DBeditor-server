package models.entity

import java.util
import javax.persistence._

import io.ebean.annotation.NotNull

@Entity
@Table(name = "\"user\"")
class User extends BaseModel {
    @NotNull
    @Lob
    var username: String = _

    @NotNull
    @Lob
    var email: String = _

    @NotNull
    @Lob
    var password: String = _

    @OneToMany(cascade = Array(CascadeType.ALL), mappedBy = "creator", fetch = FetchType.LAZY)
    var databases: util.List[Database] = new util.LinkedList[Database]
}