package br.com.igor.todolist.task;

import br.com.igor.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.events.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel tasnkModel, HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        tasnkModel.setIdUser((UUID) idUser);

        //Validação de Data
        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(tasnkModel.getStartAt()) || currentDate.isAfter(tasnkModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de início / data de término deve ser maior do que a data atual");
        }

        if (tasnkModel.getStartAt().isAfter(tasnkModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de início deve ser maior do que a data de término ");
        }
        var task = this.taskRepository.save(tasnkModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    //Listagem de Tarefa
    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        var tasks = this.taskRepository.findByIdUser((UUID) idUser);
        return tasks;
    }

    //Update de Tarefas
    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id,  HttpServletRequest request) {

        var idUser = request.getAttribute("idUser");
        var task = this.taskRepository.findById(id).orElse(null);

        Utils.copyNonNUllProperties(taskModel, task);

        //Verificando se a tarefa existe
        if(task == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Tarefa não Encontrada");
        }

        //Validando Usuário Dono
        if (!task.getIdUser().equals(idUser)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não tem permissão para alter essa tarefa");
        }

        var taskUpdated = this.taskRepository.save(task);
        return ResponseEntity.ok().body(this.taskRepository.save(taskUpdated));
    }


}

