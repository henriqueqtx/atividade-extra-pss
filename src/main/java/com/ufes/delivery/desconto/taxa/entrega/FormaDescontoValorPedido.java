package com.ufes.delivery.desconto.taxa.entrega;

import com.ufes.delivery.model.CupomDescontoEntrega;
import com.ufes.delivery.model.Pedido;

public class FormaDescontoValorPedido implements IFormaDescontoTaxaEntrega {
    private static final double LIMITE_VALOR_PEDIDO = 200.00;
    private static final double VALOR_DESCONTO = 5.00;

    @Override
    public CupomDescontoEntrega calcularDesconto(Pedido pedido) {
        if (seAplica(pedido) == false) {
            return new CupomDescontoEntrega("Desconto Valor Pedido", 0);
        }

        return new CupomDescontoEntrega("Desconto Valor Pedido", VALOR_DESCONTO);

    }

    @Override
    public boolean seAplica(Pedido pedido) {
        return pedido.getValorPedido() > LIMITE_VALOR_PEDIDO;
    }
}
