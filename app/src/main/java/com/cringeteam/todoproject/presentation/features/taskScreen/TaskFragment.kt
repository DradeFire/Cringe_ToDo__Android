package com.cringeteam.todoproject.presentation.features.taskScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cringeteam.todoproject.R
import com.cringeteam.todoproject.common.consts.Consts
import com.cringeteam.todoproject.common.logger.Logger
import com.cringeteam.todoproject.common.state.ScreenState
import com.cringeteam.todoproject.databinding.FragmentTaskBinding
import com.cringeteam.todoproject.presentation.base.BaseFragment
import com.cringeteam.todoproject.presentation.features.taskScreen.recyclerView.SubtasksAdapter
import com.cringeteam.todoproject.presentation.features.tasksScreen.TasksFragmentDirections
import com.cringeteam.todoproject.presentation.model.task.TaskVO
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class TaskFragment : BaseFragment<FragmentTaskBinding, TaskViewModel>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTaskBinding =
        FragmentTaskBinding::inflate
    override val viewModelClass: Class<TaskViewModel> = TaskViewModel::class.java
    override val screenName: String = SCREEN_NAME

    private val args: TaskFragmentArgs by navArgs()

    private val subtasksAdapter = SubtasksAdapter { id -> navigateToTaskScreen(id) }

    override fun initUI() {
        super.initUI()
        // TODO: add listeners for buttons
    }

    override fun initRecycler() {
        super.initRecycler()
        binding?.subtasks?.adapter = subtasksAdapter
    }

    override fun initObservers() {
        super.initObservers()
        val id = args.id

        viewModel?.let { viewModel ->
            compositeDisposable?.addAll(
                viewModel.screenState
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { state ->
                            binding?.apply {
                                reactToScreenState(progressBar, state)
                            }
                        },
                        { error ->
                            Logger.log("LoginFragment::initObservers(), screenState error: ${error.localizedMessage}")
                        }
                    ),
                Single.zip(
                    viewModel.getTaskInfo(id),
                    viewModel.getSubtasks(id)
                ) { task, subtasks ->
                    Pair(task, subtasks)
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        viewModel.screenState.onNext(ScreenState.Loading)
                    }
                    .subscribe(
                        { pair ->
                            val task = pair.first
                            val subtasks = pair.second

                            binding?.let { binding ->
                                setDataForUI(binding, task, subtasks)
                            }
                            viewModel.screenState.onNext(ScreenState.Success)
                        },
                        { error ->
                            Logger.log(
                                "TaskFragment::initObservers(), viewModel.getTaskInfo(id)," +
                                        "viewModel.getSubtasks(id)(), error: ${error.localizedMessage}"
                            )
                            viewModel.screenState.onNext(ScreenState.Error)
                        }
                    )
            )
        }
    }

    private fun reactToScreenState(progressBar: ProgressBar, state: ScreenState) {
        when (state) {
            ScreenState.Waiting -> {
                Logger.log("State is waiting")
                progressBar.isVisible = false
            }

            ScreenState.Loading -> {
                Logger.log("State is loading")
                progressBar.isVisible = true
            }

            ScreenState.Success -> {
                Logger.log("State is success")
                progressBar.isVisible = false
            }

            ScreenState.Error -> TODO("Add Error state and show error toast")
        }
    }

    private fun setDataForUI(binding:FragmentTaskBinding, task: TaskVO, subtasks: List<TaskVO>) {
        binding.title.text = task.title
        binding.description.text = task.description
        binding.notificationText.text = task.notification

        if (task.priority in Consts.HIGH_PRIORITY..Consts.LOW_PRIORITY) {
            binding.priorityText.text =
                getString(R.string.priority_task_text, task.priority)
        }
        when (task.priority) {
            Consts.HIGH_PRIORITY -> binding.priorityPicker.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.red)
            )

            Consts.MEDIUM_PRIORITY -> binding.priorityPicker.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.yellow)
            )

            Consts.LOW_PRIORITY -> binding.priorityPicker.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.black)
            )

            else -> binding.priorityPicker.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
        }

        if (task.deadline != Consts.NO_DEADLINE) {
            binding.alarmText.text = task.deadline.toString()
        }

        subtasksAdapter.setData(subtasks)
    }

    private fun navigateToTaskScreen(id: Long) {
        val action = TasksFragmentDirections.navigateTasksScreenToTaskScreen(id)
        findNavController().navigate(action)
    }

    companion object {
        private const val SCREEN_NAME = "task screen"
    }
}