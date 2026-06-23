package com.ufes.delivery.desconto.taxa.entrega;

import com.ufes.delivery.model.CupomDescontoEntrega;
import com.ufes.delivery.model.Pedido;

public interface IFormaDescontoTaxaEntrega {
    CupomDescontoEntrega calcularDesconto(Pedido pedido);

    boolean seAplica(Pedido pedido);
}
