import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.data.PizzaTamanho

class PizzaTamanhoAdapter(
    private val lista: List<PizzaTamanho>,
    private val onClick: (PizzaTamanho) -> Unit
) : RecyclerView.Adapter<PizzaTamanhoAdapter.TamanhoViewHolder>() {

    inner class TamanhoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imgPizza)
        val txtNome: TextView = itemView.findViewById(R.id.txtNome)
        val txtDetalhes: TextView = itemView.findViewById(R.id.txtDetalhes)
        val txtPreco: TextView = itemView.findViewById(R.id.txtPreco)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TamanhoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pizza_tamanho, parent, false)
        return TamanhoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TamanhoViewHolder, position: Int) {
        val item = lista[position]

        holder.txtNome.text = item.nome
        holder.txtDetalhes.text = item.descricao
        holder.txtPreco.text = item.preco

        // Carregar imagem (Firebase -> URL/base64)
        Glide.with(holder.itemView.context)
            .load(item.imagem)
            .placeholder(R.drawable.ic_pizza)
            .into(holder.img)

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = lista.size
}
