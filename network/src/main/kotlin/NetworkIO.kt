import com.google.gson.Gson
import java.io.File

class NetworkIO {
    private val gson = Gson()

    fun save(nw: Network, fname: String) {
        val json = gson.toJson(nw)
        File(fname).writeText(json)
    }

    fun load(fname: String): Network? {
        val f = File(fname)
        if (!f.exists()) return null
        val json = f.readText()
        return gson.fromJson(json, Network::class.java)
    }
}