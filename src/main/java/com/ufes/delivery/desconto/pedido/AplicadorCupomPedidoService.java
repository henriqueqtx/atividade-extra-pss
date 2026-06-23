package com.ufes.delivery.desconto.pedido;


import com.mycompany.logsauditoria.interfaces.ILogger;
import com.mycompany.logsauditoria.LogEntry;
import com.ufes.delivery.util.UsuarioLogadoService; 
import java.time.LocalDate;
import java.time.LocalTime;

import com.ufes.delivery.model.CupomDescontoPedido;
import com.ufes.delivery.model.Pedido;
import com.ufes.delivery.repository.ICupomRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AplicadorCupomPedidoService {
    private ICupomRepository cupomRepository;
    private ILogger logger;

    public AplicadorCupomPedidoService(ICupomRepository cupomRepository, ILogger logger) {
        this.cupomRepository = Objects.requireNonNull(cupomRepository, "Repositorio de cupons nao pode ser nulo");
        this.logger = logger;
    }

    public void aplicarCupom(Pedido pedido, String codigoCupom, LocalDateTime dataHoraAplicacao) {
        Objects.requireNonNull(pedido, "Pedido nao pode ser nulo");
        Objects.requireNonNull(dataHoraAplicacao, "Data e hora de aplicacao nao podem ser nulas");

        if (codigoCupom == null || codigoCupom.isBlank()) {
            throw new IllegalArgumentException("Codigo do cupom nao pode ser vazio");
        }

        Optional<CupomDescontoPedido> cupomEncontrado = cupomRepository.buscarCupom(codigoCupom);

        if (cupomEncontrado.isEmpty()) {
            throw new IllegalArgumentException("Cupom inexistente: " + codigoCupom);
        }

        CupomDescontoPedido cupom = cupomEncontrado.get();

        if (dataHoraAplicacao.isBefore(cupom.getDataHoraInicio())
                || dataHoraAplicacao.isAfter(cupom.getDataHoraFim())) {
            throw new IllegalStateException("O pedido nao esta dentro da validade do cupom");
        }

        Optional<CupomDescontoPedido> cupomAtual = pedido.getCupomAplicado();

        if (cupomAtual.isPresent()) {
            if (cupom.getPercentual() <= cupomAtual.get().getPercentual()) {
                throw new IllegalStateException(
                        "O cupom " + codigoCupom + " nao tem um percentual maior que o cupom atual");
            }
        }

        pedido.setCupomAplicado(cupom);
        
        // Prepara e grava o log
        Map<String, String> dadosExtra = new HashMap<>();
        dadosExtra.put("codigo_pedido", pedido.getCodigo());
        dadosExtra.put("nome_cliente", pedido.getCliente().getNome());

        try {
            LogEntry log = new LogEntry(
                UsuarioLogadoService.getNomeUsuario(),
                LocalDate.now(),
                LocalTime.now(),
                "Aplicação de cupom de desconto no valor total do pedido (aplicarCupom)",
                dadosExtra
            );
            logger.registrar(log);
        } catch (Exception e) {
            System.out.println("Erro ao gravar log: " + e.getMessage());
        }
    }
}
