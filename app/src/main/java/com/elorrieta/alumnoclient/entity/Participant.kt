import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elorrieta.alumnoclient.entity.Meeting
import com.elorrieta.alumnoclient.entity.User
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.sql.Timestamp

@JsonIgnoreProperties(ignoreUnknown = true, value = ["hibernateLazyInitializer"])
data class Participant(
    val id: Long = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val meeting: Meeting? = null,
    val user: User,
    val status: String
)
