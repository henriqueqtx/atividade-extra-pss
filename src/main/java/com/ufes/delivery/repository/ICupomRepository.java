package com.ufes.delivery.repository;

import com.ufes.delivery.model.CupomDescontoPedido;
import java.util.Optional;

public interface ICupomRepository {
    Optional<CupomDescontoPedido> buscarCupom(String codigo);
}
