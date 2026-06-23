package com.ufes.delivery.configuracao;

public final class ConfiguracaoService {
    private static final double TAXA_ENTREGA_PADRAO = 10.00;

    private ConfiguracaoService() {
    }

    public static double getTaxaEntregaPadrao() {
        return TAXA_ENTREGA_PADRAO;
    }
}
