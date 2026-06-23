package com.ufes.delivery.desconto.taxa.entrega;

import com.ufes.delivery.model.CupomDescontoEntrega;
import com.ufes.delivery.model.Item;
import com.ufes.delivery.model.Pedido;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class FormaDescontoTipoItem implements IFormaDescontoTaxaEntrega {
    private Map<String, Double> descontosPorTipoItem;

    public FormaDescontoTipoItem() {
        descontosPorTipoItem = new HashMap<>();

        descontosPorTipoItem.put("Alimentacao", 5.00);
        descontosPorTipoItem.put("Educacao", 2.00);
        descontosPorTipoItem.put("Lazer", 1.50);
    }

    @Override
    public CupomDescontoEntrega calcularDesconto(Pedido pedido) {
        double valorDesconto = 0;

        if (seAplica(pedido) == false) {
            return new CupomDescontoEntrega("Desconto Tipo de Item", valorDesconto);
        }

        Set<String> tiposJaConsiderados = new HashSet<>();

        for (Item item : pedido.getItens()) {
            Optional<Double> descontoPorTipo = buscarDesconto(item);

            if (descontoPorTipo.isPresent() && !tiposJaConsiderados.contains(item.getTipo())) {
                valorDesconto += descontoPorTipo.get();
                tiposJaConsiderados.add(item.getTipo());
            }
        }

        return new CupomDescontoEntrega("Desconto Tipo de Item", valorDesconto);
    }

    @Override
    public boolean seAplica(Pedido pedido) {
        for (Item item : pedido.getItens()) {
            if (buscarDesconto(item).isPresent()) {
                return true;
            }
        }
        return false;
    }

    private Optional<Double> buscarDesconto(Item item) {
        return Optional.ofNullable(descontosPorTipoItem.get(item.getTipo()));
    }
}
