package de.unistuttgart.towercrushbackend.data.mapper;

import de.unistuttgart.towercrushbackend.data.Configuration;
import de.unistuttgart.towercrushbackend.data.ConfigurationDTO;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * This mapper maps the ConfigurationDTO objects (used from external clients) and Configuration objects (used from internal code)
 */
@Mapper(componentModel = "spring")
public interface ConfigurationMapper {
    ConfigurationDTO configurationToConfigurationDTO(final Configuration configuration);

    Configuration configurationDTOToConfiguration(final ConfigurationDTO configurationDTO);

    List<ConfigurationDTO> configurationsToConfigurationDTOs(final List<Configuration> configurations);
}
