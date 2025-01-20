import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "participants")
data class Participant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val meetingId: Long,
    val userId: Long,
    val status: String       // Representa el campo status (debe ser una de las opciones de ENUM)
)
